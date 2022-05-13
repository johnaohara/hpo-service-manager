import React, { ComponentProps } from 'react';
import { Experiments } from '@app/Experiments/Experiments';
import { Story } from '@storybook/react';

//👇 This default export determines where your story goes in the story list
export default {
  title: 'Components/Dashboard',
  component: Experiments,
};

//👇 We create a “template” of how args map to rendering
const Template: Story<ComponentProps<typeof Experiments>> = (args) => <Experiments {...args} />;

export const FirstStory = Template.bind({});
FirstStory.args = {
  /*👇 The args you need here will depend on your component */
};
